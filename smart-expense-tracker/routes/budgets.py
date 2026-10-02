from flask import Blueprint, request, jsonify, session
from models.budget import Budget
from datetime import datetime

budget_bp = Blueprint('budgets', __name__)

def get_current_user_id():
    return session.get('user_id', 1)

@budget_bp.route('/api/budgets', methods=['GET'])
def list_budgets():
    uid = get_current_user_id()
    now = datetime.now()
    month = int(request.args.get('month', now.month))
    year = int(request.args.get('year', now.year))

    budgets = Budget.get_all(uid, month, year)
    return jsonify({"success": True, "data": budgets})

@budget_bp.route('/api/budgets', methods=['POST'])
def save_budget():
    uid = get_current_user_id()
    data = request.get_json() or {}
    category = data.get('category')
    amount = data.get('amount')
    now = datetime.now()
    month = int(data.get('month', now.month))
    year = int(data.get('year', now.year))

    if not category or not amount:
        return jsonify({"success": False, "error": "Category and amount are required"}), 400

    b = Budget.create(uid, category, amount, month, year)
    return jsonify({"success": True, "data": b}), 201

@budget_bp.route('/api/budgets/<int:b_id>', methods=['DELETE'])
def delete_budget(b_id):
    uid = get_current_user_id()
    deleted = Budget.delete(b_id, uid)
    if not deleted:
        return jsonify({"success": False, "error": "Budget not found"}), 404
    return jsonify({"success": True, "message": "Budget deleted"})
