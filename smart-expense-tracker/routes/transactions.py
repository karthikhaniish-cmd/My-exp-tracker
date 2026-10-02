from flask import Blueprint, request, jsonify, session, Response
from models.transaction import Transaction
from services.category_service import CategoryService
from services.export_service import ExportService

tx_bp = Blueprint('transactions', __name__)

def get_current_user_id():
    # If not logged in, fallback to demo user ID 1
    return session.get('user_id', 1)

@tx_bp.route('/api/transactions', methods=['GET'])
def list_transactions():
    uid = get_current_user_id()
    t_type = request.args.get('type')
    category = request.args.get('category')
    search = request.args.get('search')
    sort = request.args.get('sort', 'newest')

    txs = Transaction.get_all(uid, t_type=t_type, category=category, search=search, sort=sort)
    return jsonify({"success": True, "data": txs})

@tx_bp.route('/api/transactions', methods=['POST'])
def add_transaction():
    uid = get_current_user_id()
    data = request.get_json() or {}

    t_type = data.get('type', 'EXPENSE')
    amount = data.get('amount')
    description = data.get('description', '')
    category = data.get('category', 'Other')
    payment_method = data.get('payment_method', 'Cash')
    date = data.get('date')
    notes = data.get('notes', '')

    if not amount or not description or not date:
        return jsonify({"success": False, "error": "Amount, description, and date are required"}), 400

    new_tx = Transaction.create(uid, t_type, amount, description, category, payment_method, date, notes)
    return jsonify({"success": True, "data": new_tx}), 201

@tx_bp.route('/api/transactions/<int:t_id>', methods=['PUT'])
def update_transaction(t_id):
    uid = get_current_user_id()
    data = request.get_json() or {}

    t_type = data.get('type', 'EXPENSE')
    amount = data.get('amount')
    description = data.get('description', '')
    category = data.get('category', 'Other')
    payment_method = data.get('payment_method', 'Cash')
    date = data.get('date')
    notes = data.get('notes', '')

    updated = Transaction.update(t_id, uid, t_type, amount, description, category, payment_method, date, notes)
    if not updated:
        return jsonify({"success": False, "error": "Transaction not found"}), 404
    return jsonify({"success": True, "data": updated})

@tx_bp.route('/api/transactions/<int:t_id>', methods=['DELETE'])
def delete_transaction(t_id):
    uid = get_current_user_id()
    deleted = Transaction.delete(t_id, uid)
    if not deleted:
        return jsonify({"success": False, "error": "Transaction not found"}), 404
    return jsonify({"success": True, "message": "Transaction deleted"})

@tx_bp.route('/api/transactions/detect-category', methods=['POST'])
def detect_category():
    data = request.get_json() or {}
    desc = data.get('description', '')
    result = CategoryService.detect(desc)
    return jsonify({"success": True, "match": result})

@tx_bp.route('/api/export/csv', methods=['GET'])
def export_csv():
    uid = get_current_user_id()
    txs = Transaction.get_all(uid)
    csv_data = ExportService.to_csv(txs)
    return Response(
        csv_data,
        mimetype="text/csv",
        headers={"Content-disposition": "attachment; filename=expenses.csv"}
    )

@tx_bp.route('/api/export/json', methods=['GET'])
def export_json():
    uid = get_current_user_id()
    txs = Transaction.get_all(uid)
    json_data = ExportService.to_json(txs)
    return Response(
        json_data,
        mimetype="application/json",
        headers={"Content-disposition": "attachment; filename=expenses.json"}
    )

@tx_bp.route('/api/import/csv', methods=['POST'])
def import_csv():
    uid = get_current_user_id()
    content = ""
    if 'file' in request.files:
        content = request.files['file'].read().decode('utf-8')
    elif request.is_json:
        content = request.get_json().get('csv_text', '')

    res = ExportService.parse_and_validate_csv(content)
    if not res['valid']:
        return jsonify({"success": False, "errors": res['errors']}), 400

    imported = []
    for item in res['transactions']:
        t = Transaction.create(
            uid, item['type'], item['amount'], item['description'],
            item['category'], item['payment_method'], item['date'], item['notes']
        )
        imported.append(t)

    return jsonify({"success": True, "count": len(imported), "data": imported})
