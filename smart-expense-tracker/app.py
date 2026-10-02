import os
from flask import Flask, render_template, session, redirect, url_for
from config import Config
from models.base import init_db
from models.user import User
from models.transaction import Transaction
from models.budget import Budget
from models.recurring import Recurring
from routes.auth import auth_bp
from routes.transactions import tx_bp
from routes.budgets import budget_bp
from routes.analytics import analytics_bp
from routes.insights import insights_bp
from routes.recurring import recurring_bp
from datetime import datetime, timedelta

app = Flask(__name__)
app.config.from_object(Config)

# Register Blueprints
app.register_blueprint(auth_bp)
app.register_blueprint(tx_bp)
app.register_blueprint(budget_bp)
app.register_blueprint(analytics_bp)
app.register_blueprint(insights_bp)
app.register_blueprint(recurring_bp)

# Auto seed default demo user and records on startup
def seed_initial_data():
    init_db()
    demo_user = User.find_by_email("demo@smartfinance.com")
    if not demo_user:
        demo_user = User.create("Alex Mercer", "demo@smartfinance.com", "Password@123")
        uid = demo_user['id']

        today = datetime.now()
        cur_year = today.year
        cur_month = today.month

        # Demo Transactions
        Transaction.create(uid, "INCOME", 60000, "Monthly Salary Credited", "Salary", "Bank Transfer", (today - timedelta(days=1)).strftime("%Y-%m-%d"), "Direct deposit")
        Transaction.create(uid, "INCOME", 12000, "Client UI Freelance", "Freelance", "UPI", (today - timedelta(days=4)).strftime("%Y-%m-%d"), "Mobile App Redesign")
        Transaction.create(uid, "EXPENSE", 5200, "Swiggy & Weekend Dinner", "Food", "UPI", (today - timedelta(days=1)).strftime("%Y-%m-%d"), "Italian restaurant")
        Transaction.create(uid, "EXPENSE", 2100, "Uber commute & metro", "Transport", "UPI", (today - timedelta(days=2)).strftime("%Y-%m-%d"), "Office commute")
        Transaction.create(uid, "EXPENSE", 3500, "Amazon headphones", "Shopping", "Credit Card", (today - timedelta(days=3)).strftime("%Y-%m-%d"), "Noise cancelling earphones")
        Transaction.create(uid, "EXPENSE", 4000, "Electricity & Fiber Bill", "Bills", "Bank Transfer", (today - timedelta(days=5)).strftime("%Y-%m-%d"), "Monthly utilities")
        Transaction.create(uid, "EXPENSE", 1500, "Netflix & IMAX Movie", "Entertainment", "Credit Card", (today - timedelta(days=6)).strftime("%Y-%m-%d"), "Cinema tickets")

        # Demo Budgets
        Budget.create(uid, "Food", 7000, cur_month, cur_year)
        Budget.create(uid, "Transport", 3000, cur_month, cur_year)
        Budget.create(uid, "Shopping", 5000, cur_month, cur_year)
        Budget.create(uid, "Bills", 4500, cur_month, cur_year)
        Budget.create(uid, "Entertainment", 2000, cur_month, cur_year)

        # Demo Recurring
        Recurring.create(uid, "Netflix Premium", 649, "Entertainment", "MONTHLY", (today - timedelta(days=15)).strftime("%Y-%m-%d"), (today + timedelta(days=15)).strftime("%Y-%m-%d"))
        Recurring.create(uid, "Apartment Rent", 12000, "Bills", "MONTHLY", (today - timedelta(days=20)).strftime("%Y-%m-%d"), (today + timedelta(days=10)).strftime("%Y-%m-%d"))
        Recurring.create(uid, "Gym Membership", 1500, "Health", "MONTHLY", (today - timedelta(days=10)).strftime("%Y-%m-%d"), (today + timedelta(days=20)).strftime("%Y-%m-%d"))

with app.app_context():
    seed_initial_data()

# Template Page Routes
@app.route('/')
def main_dashboard():
    return render_template('dashboard.html', active_page='dashboard')

@app.route('/transactions')
def transactions_page():
    return render_template('transactions.html', active_page='transactions')

@app.route('/budgets')
def budgets_page():
    return render_template('budgets.html', active_page='budgets')

@app.route('/analytics')
def analytics_page():
    return render_template('analytics.html', active_page='analytics')

@app.route('/recurring')
def recurring_page():
    return render_template('recurring.html', active_page='recurring')

@app.route('/insights')
def insights_page():
    return render_template('insights.html', active_page='insights')

@app.route('/settings')
def settings_page():
    return render_template('settings.html', active_page='settings')

if __name__ == '__main__':
    port = int(os.environ.get("PORT", 5000))
    app.run(host='0.0.0.0', port=port, debug=True)
