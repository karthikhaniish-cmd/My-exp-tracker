from datetime import datetime
from collections import defaultdict

class AnalyticsService:
    @staticmethod
    def calculate_dashboard_metrics(transactions):
        total_income = sum(t['amount'] for t in transactions if t['type'] == 'INCOME')
        total_expenses = sum(t['amount'] for t in transactions if t['type'] == 'EXPENSE')
        total_balance = total_income - total_expenses
        net_savings = total_balance

        now = datetime.now()
        cur_month_str = now.strftime("%Y-%m")

        cur_month_expenses = [
            t for t in transactions
            if t['type'] == 'EXPENSE' and t['date'].startswith(cur_month_str)
        ]
        monthly_spending = sum(t['amount'] for t in cur_month_expenses)

        # Highest spending category
        cat_sums = defaultdict(float)
        for t in cur_month_expenses:
            cat_sums[t['category']] += t['amount']

        highest_cat = "None"
        highest_amt = 0.0
        if cat_sums:
            highest_cat, highest_amt = max(cat_sums.items(), key=lambda x: x[1])

        savings_rate = (net_savings / total_income * 100) if total_income > 0 else 0.0

        return {
            "total_balance": round(total_balance, 2),
            "total_income": round(total_income, 2),
            "total_expenses": round(total_expenses, 2),
            "net_savings": round(net_savings, 2),
            "monthly_spending": round(monthly_spending, 2),
            "highest_spending_category": highest_cat,
            "highest_category_amount": round(highest_amt, 2),
            "savings_rate": round(savings_rate, 1)
        }

    @staticmethod
    def get_chart_data(transactions):
        # 1. Monthly expenses line chart (Jan-Dec)
        cur_year = str(datetime.now().year)
        month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
        monthly_expenses = [0.0] * 12
        monthly_income = [0.0] * 12

        for t in transactions:
            if t['date'].startswith(cur_year):
                try:
                    m_idx = int(t['date'].split('-')[1]) - 1
                    if 0 <= m_idx < 12:
                        if t['type'] == 'EXPENSE':
                            monthly_expenses[m_idx] += t['amount']
                        elif t['type'] == 'INCOME':
                            monthly_income[m_idx] += t['amount']
                except Exception:
                    pass

        # 2. Categories Doughnut Chart
        cat_breakdown = defaultdict(float)
        for t in transactions:
            if t['type'] == 'EXPENSE':
                cat_breakdown[t['category']] += t['amount']

        # 3. Income vs Expense overall
        total_inc = sum(t['amount'] for t in transactions if t['type'] == 'INCOME')
        total_exp = sum(t['amount'] for t in transactions if t['type'] == 'EXPENSE')

        return {
            "monthly_chart": {
                "labels": month_names,
                "expenses": monthly_expenses,
                "income": monthly_income
            },
            "category_doughnut": {
                "labels": list(cat_breakdown.keys()),
                "data": list(cat_breakdown.values())
            },
            "income_vs_expense": {
                "labels": ["Total Income", "Total Expenses"],
                "data": [total_inc, total_exp]
            }
        }
