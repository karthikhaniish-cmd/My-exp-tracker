import os
import requests
from config import Config
from collections import defaultdict
from datetime import datetime

class AiService:
    @staticmethod
    def generate_insights(transactions, budgets=None, recurring=None, currency="₹"):
        if not transactions:
            return [{
                "title": "Welcome to Smart Expense Tracker",
                "message": "Add your first transaction to unlock deep automated spending analytics.",
                "type": "info"
            }]

        # Check if AI key is configured
        api_key = Config.AI_API_KEY
        if api_key and api_key != "your_ai_api_key" and api_key != "your_gemini_api_key_here":
            try:
                ai_result = AiService._call_gemini_api(api_key, transactions, currency)
                if ai_result:
                    return ai_result
            except Exception as e:
                pass # Fallback to rule engine

        return AiService._rule_based_insights(transactions, budgets, recurring, currency)

    @staticmethod
    def _rule_based_insights(transactions, budgets, recurring, currency):
        insights = []
        now = datetime.now()
        cur_month_str = now.strftime("%Y-%m")
        prev_month = now.month - 1 if now.month > 1 else 12
        prev_year = now.year if now.month > 1 else now.year - 1
        prev_month_str = f"{prev_year}-{prev_month:02d}"

        cur_month_tx = [t for t in transactions if t['date'].startswith(cur_month_str)]
        prev_month_tx = [t for t in transactions if t['date'].startswith(prev_month_str)]

        cur_income = sum(t['amount'] for t in cur_month_tx if t['type'] == 'INCOME')
        cur_expense = sum(t['amount'] for t in cur_month_tx if t['type'] == 'EXPENSE')
        prev_expense = sum(t['amount'] for t in prev_month_tx if t['type'] == 'EXPENSE')

        # 1. Savings Rate / Balance rule
        if cur_income > 0:
            savings = cur_income - cur_expense
            savings_rate = (savings / cur_income) * 100
            if savings_rate >= 40:
                insights.append({
                    "title": "Superb Savings Discipline",
                    "message": f"Your current savings rate is {savings_rate:.1f}%! Outstanding financial management.",
                    "type": "positive"
                })
            elif savings_rate < 0:
                insights.append({
                    "title": "Deficit Alert: Expenses Exceed Income",
                    "message": f"You have spent {currency}{abs(savings):,.0f} more than you earned this month. Review your discretionary spending.",
                    "type": "alert"
                })

        # 2. Month-over-month category spending spike
        cur_cat_expenses = defaultdict(float)
        for t in cur_month_tx:
            if t['type'] == 'EXPENSE':
                cur_cat_expenses[t['category']] += t['amount']

        prev_cat_expenses = defaultdict(float)
        for t in prev_month_tx:
            if t['type'] == 'EXPENSE':
                prev_cat_expenses[t['category']] += t['amount']

        if cur_cat_expenses:
            top_cat, top_amt = max(cur_cat_expenses.items(), key=lambda x: x[1])
            if cur_expense > 0:
                pct = (top_amt / cur_expense) * 100
                if pct >= 35:
                    insights.append({
                        "title": f"Heavy Concentration in {top_cat}",
                        "message": f"Your largest spending category is {top_cat} ({currency}{top_amt:,.0f}), accounting for {pct:.1f}% of this month's expenses.",
                        "type": "warning" if pct >= 50 else "info"
                    })

        for cat, cur_amt in cur_cat_expenses.items():
            prev_amt = prev_cat_expenses.get(cat, 0.0)
            if prev_amt > 0 and cur_amt > prev_amt * 1.25:
                spike = ((cur_amt - prev_amt) / prev_amt) * 100
                insights.append({
                    "title": f"{cat} Expenses Rose by {spike:.0f}%",
                    "message": f"You spent {currency}{cur_amt:,.0f} on {cat} this month compared with {currency}{prev_amt:,.0f} last month.",
                    "type": "warning"
                })
                break

        # 3. Weekend vs Weekday analysis
        weekend_exp = 0.0
        for t in cur_month_tx:
            if t['type'] == 'EXPENSE':
                try:
                    dt = datetime.strptime(t['date'], "%Y-%m-%d")
                    if dt.weekday() >= 5: # Saturday or Sunday
                        weekend_exp += t['amount']
                except Exception:
                    pass

        if cur_expense > 0 and (weekend_exp / cur_expense) >= 0.40:
            w_pct = (weekend_exp / cur_expense) * 100
            insights.append({
                "title": "Weekend Spending Trend",
                "message": f"{w_pct:.0f}% of your monthly expenses happen on Saturdays and Sundays. Plan weekend activities with predefined limits.",
                "type": "info"
            })

        return insights

    @staticmethod
    def _call_gemini_api(api_key, transactions, currency):
        url = f"https://generativelanguage.googleapis.com/v1beta/models/{Config.AI_MODEL}:generateContent?key={api_key}"
        recent = transactions[:20]
        tx_summary = "\n".join([f"- {t['date']}: {t['type']} {currency}{t['amount']} for {t['description']} ({t['category']})" for t in recent])

        prompt = f"""
        Analyze the following personal finance transactions and provide 3 concise, bulleted fintech insights:
        {tx_summary}
        Return a JSON array of objects with keys: title, message, type (positive, warning, alert, info). Return ONLY JSON.
        """

        payload = {"contents": [{"parts": [{"text": prompt}]}]}
        res = requests.post(url, json=payload, timeout=8)
        if res.status_code == 200:
            import json
            text = res.json()['candidates'][0]['content']['parts'][0]['text'].strip()
            if text.startswith("```json"):
                text = text[7:-3].strip()
            elif text.startswith("```"):
                text = text[3:-3].strip()
            return json.loads(text)
        return None
