// Transactions history controller
let currentTransactions = [];

async function loadTransactions() {
    const search = document.getElementById('search-input')?.value || '';
    const type = document.getElementById('filter-type')?.value || '';
    const category = document.getElementById('filter-category')?.value || '';
    const sort = document.getElementById('filter-sort')?.value || 'newest';

    const url = `/api/transactions?search=${encodeURIComponent(search)}&type=${encodeURIComponent(type)}&category=${encodeURIComponent(category)}&sort=${encodeURIComponent(sort)}`;

    try {
        const res = await fetch(url);
        const data = await res.json();
        if (data.success) {
            currentTransactions = data.data;
            renderTransactionTable(currentTransactions);
        }
    } catch (err) {
        console.error("Load transactions error:", err);
    }
}

function renderTransactionTable(list) {
    const tbody = document.getElementById('transactions-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No transactions found</td></tr>`;
        return;
    }

    list.forEach(tx => {
        const isIncome = tx.type === 'INCOME';
        const tr = document.createElement('tr');
        tr.className = 'table-row-item';
        tr.innerHTML = `
            <td>${tx.date}</td>
            <td><strong>${tx.description}</strong><br><small class="text-muted">${tx.notes || ''}</small></td>
            <td><span class="badge bg-dark border">${tx.category}</span></td>
            <td>${tx.payment_method}</td>
            <td><span class="${isIncome ? 'badge-income' : 'badge-expense'}">${tx.type}</span></td>
            <td class="fw-bold ${isIncome ? 'text-success' : 'text-danger'}">
                ${isIncome ? '+' : '-'}${App.formatCurrency(tx.amount)}
            </td>
            <td>
                <button class="btn btn-sm btn-fintech-outline" onclick="deleteTransaction(${tx.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function deleteTransaction(id) {
    if (!confirm("Are you sure you want to delete this transaction?")) return;
    try {
        const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            App.showToast("Transaction deleted");
            loadTransactions();
        }
    } catch (e) {
        console.error(e);
    }
}

async function saveTransactionForm(e) {
    e.preventDefault();
    const type = document.getElementById('tx-type').value;
    const amount = document.getElementById('tx-amount').value;
    const description = document.getElementById('tx-description').value;
    const category = document.getElementById('tx-category').value;
    const payment_method = document.getElementById('tx-payment').value;
    const date = document.getElementById('tx-date').value;
    const notes = document.getElementById('tx-notes').value;

    const payload = { type, amount, description, category, payment_method, date, notes };

    try {
        const res = await fetch('/api/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            App.showToast("Transaction saved successfully!");
            const modalEl = document.getElementById('addTransactionModal');
            if (modalEl && window.bootstrap) {
                const modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
            }
            if (typeof loadTransactions === 'function') loadTransactions();
            if (typeof loadDashboard === 'function') loadDashboard();
        } else {
            alert(data.error || "Failed to save");
        }
    } catch (err) {
        console.error(err);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('transactions-table-body')) {
        loadTransactions();
    }
    const form = document.getElementById('add-tx-form');
    if (form) form.addEventListener('submit', saveTransactionForm);
});
