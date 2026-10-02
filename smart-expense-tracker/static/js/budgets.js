// Budgets controller
async function loadBudgets() {
    try {
        const [bRes, dRes] = await Promise.all([
            fetch('/api/budgets'),
            fetch('/api/dashboard')
        ]);
        const bData = await bRes.json();
        const dData = await dRes.json();

        if (!bData.success) return;

        const container = document.getElementById('budgets-grid');
        if (!container) return;
        container.innerHTML = '';

        const expensesByCategory = {};
        if (dData.success && dData.charts.category_doughnut) {
            const labels = dData.charts.category_doughnut.labels;
            const amounts = dData.charts.category_doughnut.data;
            labels.forEach((l, i) => expensesByCategory[l] = amounts[i]);
        }

        bData.data.forEach(b => {
            const spent = expensesByCategory[b.category] || 0;
            const ratio = b.amount > 0 ? (spent / b.amount) : 0;
            const pct = Math.min(Math.round(ratio * 100), 100);

            let statusClass = "bg-success";
            let statusText = "Safe";
            if (ratio >= 1.0) {
                statusClass = "bg-danger";
                statusText = "Exceeded";
            } else if (ratio >= 0.90) {
                statusClass = "bg-danger";
                statusText = "Alert (90%+)";
            } else if (ratio >= 0.75) {
                statusClass = "bg-warning";
                statusText = "Warning (75%+)";
            }

            const col = document.createElement('div');
            col.className = 'col-md-6 col-lg-4 mb-4';
            col.innerHTML = `
                <div class="fintech-card">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <h5 class="fw-bold mb-0">${b.category}</h5>
                        <span class="badge ${statusClass}">${statusText}</span>
                    </div>
                    <div class="d-flex justify-content-between text-muted small mb-2">
                        <span>Spent: ${App.formatCurrency(spent)}</span>
                        <span>Budget: ${App.formatCurrency(b.amount)}</span>
                    </div>
                    <div class="progress" style="height: 10px; background-color: var(--surface-elevated);">
                        <div class="progress-bar ${statusClass}" role="progressbar" style="width: ${pct}%"></div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <small class="text-muted">${pct}% used</small>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteBudget(${b.id})">Remove</button>
                    </div>
                </div>
            `;
            container.appendChild(col);
        });
    } catch (e) {
        console.error("Budgets error:", e);
    }
}

async function deleteBudget(id) {
    if (!confirm("Remove this budget cap?")) return;
    try {
        const res = await fetch(`/api/budgets/${id}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            App.showToast("Budget deleted");
            loadBudgets();
        }
    } catch (e) {
        console.error(e);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('budgets-grid')) {
        loadBudgets();
    }
});
