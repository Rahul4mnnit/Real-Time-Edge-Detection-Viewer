// viewer.ts
const socket = new WebSocket("ws://10.109.240.97:8080"); // 👈 Replace with your PHONE IP
const statusE1 = document.getElementById("status") as HTMLElement;
const img = document.getElementById("frame") as HTMLImageElement;


socket.onopen = () => {
    console.log("✅ Connected to WebSocket");
    if (statusE1) statusE1.textContent = "Connected ✅";
};


socket.onerror = (e) => {
    console.error("❌ Connection error:", e);
    if (statusE1) statusE1.textContent = "Error ❌ (check IP/port)";
};

socket.onmessage = (event) => {
    if (img) img.src = "data:image/jpeg;base64," + event.data;
};