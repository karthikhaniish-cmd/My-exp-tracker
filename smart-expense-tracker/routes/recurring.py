from flask import Blueprint, request, jsonify, session
from models.recurring import Recurring

recurring_bp = Blueprint('recurring', __name__)

def get_current_user_id():
    return session.get('user_id', 1)

@recurring_bp.route('/api/recurring', methods=['GET'])
def list_recurring():
    uid = get_current_user_id()
    items = Recurring.get_all(uid)
    return jsonify({"success": True, "data": items})

@recurring_bp.route('/api/recurring', methods=['POST'])
def add_recurring():
    uid = get_current_user_id()
    data = request.get_json() or {}

    name = data.get('name')
    amount = data.get('amount')
    category = data.get('category', 'Bills')
    frequency = data.get('frequency', 'MONTHLY')
    start_date = data.get('start_date')
    next_date = data.get('next_date', start_date)

    if not name or not amount or not start_date:
        return jsonify({"success": False, "error": "Name, amount and start date are required"}), 400

    r = Recurring.create(uid, name, amount, category, frequency, start_date, next_date)
    return jsonify({"success": True, "data": r}), 201

@recurring_bp.route('/api/recurring/<int:r_id>', methods=['DELETE'])
def delete_recurring(r_id):
    uid = get_current_user_id()
    deleted = Recurring.delete(r_id, uid)
    if not deleted:
        return jsonify({"success": False, "error": "Recurring payment not found"}), 404
    return jsonify({"success": True, "message": "Recurring item deleted"})
