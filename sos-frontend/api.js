const API_BASE = "http://localhost:8080/api/v1";

export const CopService = {
    async fetchLocations() {
        const res = await fetch(`${API_BASE}/copmap/locations`);
        if (!res.ok) throw new Error("Failed to fetch locations");
        return res.json();
    },

    async deployOfficer(payload) {
        return fetch(`${API_BASE}/copmap/place-location`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
    },

    async triggerEmergency(lat, lng) {
        const res = await fetch(`${API_BASE}/alerts/sos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ officerId: "HQ-SYS", lat, lng, type: "RIOT" })
        });
        return res.json();
    }
};