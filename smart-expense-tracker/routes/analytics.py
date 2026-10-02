from flask import Blueprint, jsonify, session
from models.transaction import Transaction
from services.analytics_service import AnalyticsService

analytics_bp = Blueprint('analytics', __name__)

def get_current_user_id():
    return session.get('user_id', 1)

@analytics_bp.route('/api/dashboard', methods=['GET'])
def get_dashboard_data():
    uid = get_current_user_id()
    txs = Transaction.get_all(uid)
    metrics = AnalyticsService.calculate_dashboard_metrics(txs)
    charts = AnalyticsService.get_chart_data(txs)
    recent = txs[:5]

    return jsonify({
        "success": True,
        "metrics": metrics,
        "charts": charts,
        "recent_transactions": recent
    })

@analytics_bp.route('/api/analytics', methods=['GET'])
def get_analytics():
    uid = get_current_user_id()
    txs = Transaction.get_all(uid)
    metrics = AnalyticsService.calculate_dashboard_metrics(txs)
    charts = AnalyticsService.get_chart_data(txs)

    # Detailed Category list
    total_exp = metrics['total_expenses']
    cat_counts = {}
    cat_sums = {}
    for t in txs:
        if t['type'] == 'EXPENSE':
            c = t['category']
            cat_counts[c] = cat_counts.get(c, 0) + 1
            cat_sums[c] = cat_sums.get(c, 0.0) + t['amount']

    category_table = []
    for c, amt in sorted(cat_sums.items(), key=lambda x: x[1], reverse=True):
        pct = (amt / total_exp * 100) if total_exp > 0 else 0.0
        category_table.append({
            "category": c,
            "total_spending": amt,
            "percentage": round(pct, 1),
            "transactions_count": cat_counts[c]
        })

    return jsonify({
        "success": True,
        "metrics": metrics,
        "charts": charts,
        "category_analysis": category_table
    })
