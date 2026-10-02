// Global Smart Expense Tracker Core App JS
const App = {
    currency: localStorage.getItem('fintech_currency') || '₹',

    init() {
        this.setupTheme();
        this.setupCategoryDetection();
    },

    formatCurrency(amount) {
        return `${this.currency}${Number(amount).toLocaleString('en-IN', { maximumFractionDigits: 0 })}`;
    },

    showToast(message, duration = 3000) {
        let toast = document.getElementById('app-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'app-toast';
            toast.className = 'fintech-toast';
            document.body.appendChild(toast);
        }
        toast.innerText = message;
        toast.style.display = 'block';
        setTimeout(() => {
            toast.style.display = 'none';
        }, duration);
    },

    setupTheme() {
        const savedTheme = localStorage.getItem('fintech_theme') || 'dark';
        document.documentElement.setAttribute('data-theme', savedTheme);
    },

    toggleTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('fintech_theme', theme);
        this.showToast(`Theme switched to ${theme}`);
    },

    setupCategoryDetection() {
        const descInput = document.getElementById('tx-description');
        const catSelect = document.getElementById('tx-category');
        const typeSelect = document.getElementById('tx-type');
        const suggestionBadge = document.getElementById('smart-category-suggestion');

        if (!descInput) return;

        let debounceTimer;
        descInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            const val = e.target.value.trim();
            if (val.length < 3) {
                if (suggestionBadge) suggestionBadge.style.display = 'none';
                return;
            }

            debounceTimer = setTimeout(async () => {
                try {
                    const res = await fetch('/api/transactions/detect-category', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ description: val })
                    });
                    const data = await res.json();
                    if (data.success && data.match && suggestionBadge) {
                        suggestionBadge.innerText = `✨ Suggested: ${data.match.category} (${data.match.type})`;
                        suggestionBadge.style.display = 'inline-block';
                        suggestionBadge.onclick = () => {
                            if (catSelect) catSelect.value = data.match.category;
                            if (typeSelect) typeSelect.value = data.match.type;
                            suggestionBadge.style.display = 'none';
                        };
                    }
                } catch (err) {
                    console.error("Detection error:", err);
                }
            }, 300);
        });
    }
};

document.addEventListener('DOMContentLoaded', () => App.init());
