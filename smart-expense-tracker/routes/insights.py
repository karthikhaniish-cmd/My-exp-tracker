from flask import Blueprint, jsonify, session
from models.transaction import Transaction
from models.budget import Budget
from models.recurring import Recurring
from services.ai_service import AiService

insights_bp = Blueprint('insights', __name__)

def get_current_user_id():
    return session.get('user_id', 1)

@insights_bp.route('/api/ai/insights', methods=['GET', 'POST'])
def get_insights():
    uid = get_current_user_id()
    txs = Transaction.get_all(uid)
    budgets = Budget.get_all(uid)
    recurring = Recurring.get_all(uid)

    insights = AiService.generate_insights(txs, budgets, recurring)
    return jsonify({"success": True, "insights": insights})
