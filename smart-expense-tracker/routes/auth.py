from flask import Blueprint, request, jsonify, session, render_template, redirect, url_for
from models.user import User

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'GET':
        return render_template('register.html')

    data = request.get_json() if request.is_json else request.form
    name = data.get('name', '').strip()
    email = data.get('email', '').strip()
    password = data.get('password', '')

    if not name or not email or not password:
        msg = "All fields are required"
        return jsonify({"success": False, "error": msg}), 400 if request.is_json else render_template('register.html', error=msg)

    if User.find_by_email(email):
        msg = "Email already registered"
        return jsonify({"success": False, "error": msg}), 400 if request.is_json else render_template('register.html', error=msg)

    user = User.create(name, email, password)
    if user:
        session['user_id'] = user['id']
        session['user_name'] = user['name']
        session['user_email'] = user['email']
        if request.is_json:
            return jsonify({"success": True, "user": user})
        return redirect(url_for('main_dashboard'))

    return jsonify({"success": False, "error": "Registration failed"}), 500

@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'GET':
        return render_template('login.html')

    data = request.get_json() if request.is_json else request.form
    email = data.get('email', '').strip()
    password = data.get('password', '')

    user = User.find_by_email(email)
    if not user or not User.verify_password(user['password_hash'], password):
        msg = "Invalid email or password"
        return jsonify({"success": False, "error": msg}), 401 if request.is_json else render_template('login.html', error=msg)

    session['user_id'] = user['id']
    session['user_name'] = user['name']
    session['user_email'] = user['email']

    if request.is_json:
        return jsonify({"success": True, "user": {"id": user['id'], "name": user['name'], "email": user['email']}})
    return redirect(url_for('main_dashboard'))

@auth_bp.route('/logout', methods=['GET', 'POST'])
def logout():
    session.clear()
    if request.is_json:
        return jsonify({"success": True})
    return redirect(url_for('auth.login'))
