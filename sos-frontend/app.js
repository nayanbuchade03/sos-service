import { CopService } from './api.js';

class CommandCenter {
    constructor() {
        this.map = null;
        this.officerMarkers = new Map();
        this.alertLayer = null;
        this.dynamicIdCounter = 1000;
        
        this.initMap();
        this.bindEvents();
        this.refreshData();
    }

    initMap() {
        this.map = L.map('map').setView([18.5200, 73.8560], 14);
        L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png').addTo(this.map);
        this.alertLayer = L.layerGroup().addTo(this.map);
    }

    bindEvents() {
        document.getElementById('btn-sos').addEventListener('click', () => this.handleSOS());
        document.getElementById('btn-refresh').addEventListener('click', () => this.refreshData());
        
        this.map.on('click', (e) => this.handleMapClick(e.latlng));
    }

    async refreshData() {
        try {
            const locations = await CopService.fetchLocations();
            this.updateMapMarkers(locations);
        } catch (err) {
            this.logToFeed("Connection Error: Server unreachable", "var(--danger)");
        }
    }

    updateMapMarkers(locations) {
        this.officerMarkers.forEach(marker => this.map.removeLayer(marker));
        this.officerMarkers.clear();

        Object.entries(locations).forEach(([id, coords]) => {
            if (!coords) return;
            const marker = L.marker([coords.y, coords.x])
                .bindPopup(`<strong>Unit: ${id}</strong>`)
                .addTo(this.map);
            this.officerMarkers.set(id, marker);
        });
    }

    async handleMapClick({ lat, lng }) {
        const officerId = `OFC-${this.dynamicIdCounter++}`;
        this.logToFeed(`Deploying ${officerId}...`);

        try {
            await CopService.deployOfficer({ officerId, lat, lng });
            this.refreshData();
            this.logToFeed(`Unit ${officerId} Online`, "var(--success)");
        } catch (err) {
            this.logToFeed("Deployment failed", "var(--danger)");
        }
    }

    async handleSOS() {
        const center = this.map.getCenter();
        this.alertLayer.clearLayers();
        this.logToFeed("🚨 SOS BROADCAST INITIATED", "var(--danger)");

        try {
            const dispatches = await CopService.triggerEmergency(center.lat, center.lng);
            this.renderEmergencyUI(center, dispatches);
        } catch (err) {
            console.error(err);
        }
    }

    renderEmergencyUI(center, dispatches) {
        L.circle([center.lat, center.lng], {
            color: 'red',
            fillOpacity: 0.1,
            radius: 5000
        }).addTo(this.alertLayer);

        const feed = document.getElementById('alert-feed');
        feed.innerHTML = '';

        dispatches.forEach(d => {
            this.renderDispatchCard(d);
            this.drawDispatchLine(center, d.assignedOfficerId);
        });
    }

    renderDispatchCard(data) {
        const card = document.createElement('div');
        card.className = 'dispatch-card';
        card.innerHTML = `
            <strong>UNIT: ${data.assignedOfficerId}</strong><br>
            ETA: ${(data.distanceKm * 2).toFixed(1)} mins (${data.distanceKm.toFixed(2)} km)
        `;
        document.getElementById('alert-feed').prepend(card);
    }

    drawDispatchLine(target, officerId) {
        const marker = this.officerMarkers.get(officerId);
        if (marker) {
            L.polyline([marker.getLatLng(), target], {
                color: 'red',
                dashArray: '5, 10',
                weight: 1
            }).addTo(this.alertLayer);
        }
    }

    logToFeed(msg, color = "inherit") {
        const entry = document.createElement('div');
        entry.style.color = color;
        entry.style.padding = "4px 0";
        entry.style.borderBottom = "1px solid var(--border)";
        entry.innerText = `> ${msg}`;
        document.getElementById('alert-feed').prepend(entry);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new CommandCenter();
});