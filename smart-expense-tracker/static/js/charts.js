// Charts module for Chart.js
const FintechCharts = {
    monthlyChart: null,
    doughnutChart: null,
    incomeVsExpenseChart: null,
    trendChart: null,

    goldColor: '#C9A24B',
    emeraldColor: '#10B981',
    coralColor: '#EF4444',
    darkBorder: '#243B5B',

    initMonthlyChart(canvasId, labels, expenses, income) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return;
        if (this.monthlyChart) this.monthlyChart.destroy();

        this.monthlyChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Expenses',
                        data: expenses,
                        backgroundColor: 'rgba(239, 68, 68, 0.75)',
                        borderRadius: 6
                    },
                    {
                        label: 'Income',
                        data: income,
                        backgroundColor: 'rgba(16, 185, 129, 0.75)',
                        borderRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: '#94A3B8' } }
                },
                scales: {
                    x: { grid: { color: 'rgba(36, 59, 91, 0.3)' }, ticks: { color: '#94A3B8' } },
                    y: { grid: { color: 'rgba(36, 59, 91, 0.3)' }, ticks: { color: '#94A3B8' } }
                }
            }
        });
    },

    initCategoryDoughnut(canvasId, labels, data) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return;
        if (this.doughnutChart) this.doughnutChart.destroy();

        const colors = ['#F97316', '#3B82F6', '#EC4899', '#EAB308', '#8B5CF6', '#06B6D4', '#EF4444', '#14B8A6', '#64748B'];

        this.doughnutChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#132238'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '68%',
                plugins: {
                    legend: { position: 'right', labels: { color: '#94A3B8', boxWidth: 12 } }
                }
            }
        });
    },

    initIncomeVsExpenses(canvasId, income, expenses) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return;
        if (this.incomeVsExpenseChart) this.incomeVsExpenseChart.destroy();

        this.incomeVsExpenseChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Total Income', 'Total Expenses'],
                datasets: [{
                    data: [income, expenses],
                    backgroundColor: [this.emeraldColor, this.coralColor],
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false }, ticks: { color: '#94A3B8' } },
                    y: { grid: { color: 'rgba(36, 59, 91, 0.3)' }, ticks: { color: '#94A3B8' } }
                }
            }
        });
    },

    initSpendingTrend(canvasId, labels, points) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return;
        if (this.trendChart) this.trendChart.destroy();

        this.trendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Spending',
                    data: points,
                    borderColor: this.goldColor,
                    backgroundColor: 'rgba(201, 162, 75, 0.15)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointBackgroundColor: this.goldColor
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { color: 'rgba(36, 59, 91, 0.3)' }, ticks: { color: '#94A3B8' } },
                    y: { grid: { color: 'rgba(36, 59, 91, 0.3)' }, ticks: { color: '#94A3B8' } }
                }
            }
        });
    }
};
