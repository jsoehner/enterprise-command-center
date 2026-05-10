async function fetchData() {
    const dashboard = document.getElementById('dashboard');
    const lastUpdated = document.getElementById('last-updated');
    
    // Using the credentials configured in SecurityConfig
    const headers = new Headers();
    headers.set('Authorization', 'Basic ' + btoa('admin:password'));

    try {
        const response = await fetch('/camel/api/aggregated', { headers });
        if (!response.ok) throw new Error('Failed to fetch data');
        
        const result = await response.json();
        renderDashboard(result.data);
        lastUpdated.innerText = 'Last updated: ' + new Date(result.timestamp).toLocaleTimeString();
        
        // Fetch health status
        fetchHealth();
    } catch (error) {
        console.error(error);
        dashboard.innerHTML = `
            <div class="card" style="grid-column: 1/-1; border-color: var(--danger);">
                <div class="card-title" style="color: var(--danger);">System Error</div>
                <div class="card-value">Unable to aggregate data sources. Please check backend status.</div>
            </div>
        `;
    }
}

function renderDashboard(data) {
    const dashboard = document.getElementById('dashboard');
    dashboard.innerHTML = '';

    // User Section
    const userCard = createCard('User Identity', [
        { label: 'Name', value: data.name },
        { label: 'ID', value: data.id },
        { label: 'Email', value: data.email }
    ]);
    dashboard.appendChild(userCard);

    // Order Section
    const orderCard = createCard('Latest Order', [
        { label: 'Order ID', value: data.orderId },
        { label: 'Amount', value: `$${data.amount}` },
        { label: 'Status', value: `<span style="color: var(--success)">${data.status}</span>` }
    ]);
    dashboard.appendChild(orderCard);

    // Inventory Section
    const inventoryCard = createCard('Stock Inventory', [
        { label: 'Warehouse', value: data.warehouse },
        { label: 'Stock Count', value: data.stockCount }
    ]);
    dashboard.appendChild(inventoryCard);
}

function createCard(title, items) {
    const card = document.createElement('div');
    card.className = 'card animate-fade';
    
    let html = `<div class="card-title">${title}</div>`;
    items.forEach(item => {
        html += `
            <div class="data-row">
                <span class="data-label">${item.label}</span>
                <span class="data-value">${item.value}</span>
            </div>
        `;
    });
    
    card.innerHTML = html;
    return card;
}

async function fetchHealth() {
    const headers = new Headers();
    headers.set('Authorization', 'Basic ' + btoa('admin:password'));

    try {
        const response = await fetch('/camel/api/health', { headers });
        const healthData = await response.json();
        updateSystemStatus(healthData);
    } catch (error) {
        console.error('Health check failed', error);
    }
}

function updateSystemStatus(health) {
    // Update the main system operational badge
    const badge = document.querySelector('.status-badge');
    const isAllUp = Object.values(health).every(v => v === 'UP');
    
    if (isAllUp) {
        badge.innerHTML = '<div class="status-dot"></div>System Operational';
        badge.style.color = 'var(--success)';
        badge.style.background = 'rgba(16, 185, 129, 0.1)';
    } else {
        badge.innerHTML = '<div class="status-dot" style="background: var(--danger)"></div>Degraded Performance';
        badge.style.color = 'var(--danger)';
        badge.style.background = 'rgba(239, 68, 68, 0.1)';
    }
}

function initWebSocket() {
    const socket = new WebSocket('ws://' + window.location.hostname + ':8081/aggregated-updates');
    
    socket.onmessage = function(event) {
        console.log('Live update received');
        const result = JSON.parse(event.data);
        renderDashboard(result.data);
        document.getElementById('last-updated').innerText = 'Live Update: ' + new Date(result.timestamp).toLocaleTimeString();
        fetchHealth();
        logActivity('Event received from Kafka [api-events]. Aggregating...');
    };

    socket.onclose = function() {
        console.warn('WebSocket closed. Retrying in 5s...');
        setTimeout(initWebSocket, 5000);
    };
}

function logActivity(message) {
    const log = document.getElementById('activity-log');
    const entry = document.createElement('div');
    entry.innerText = `> [${new Date().toLocaleTimeString()}] ${message}`;
    log.prepend(entry);
    if (log.children.length > 10) log.lastElementChild.remove();
}

// Initial fetch & init
fetchData();
initWebSocket();
