from werkzeug.security import generate_password_hash, check_password_hash
from models.base import get_db

class User:
    @staticmethod
    def create(name, email, password):
        conn = get_db()
        cursor = conn.cursor()
        pwd_hash = generate_password_hash(password)
        try:
            cursor.execute(
                "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)",
                (name, email.lower().strip(), pwd_hash)
            )
            conn.commit()
            user_id = cursor.lastrowid
            return User.find_by_id(user_id)
        except Exception as e:
            return None
        finally:
            conn.close()

    @staticmethod
    def find_by_email(email):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM users WHERE email = ?", (email.lower().strip(),))
        row = cursor.fetchone()
        conn.close()
        return dict(row) if row else None

    @staticmethod
    def find_by_id(user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT id, name, email, created_at FROM users WHERE id = ?", (user_id,))
        row = cursor.fetchone()
        conn.close()
        return dict(row) if row else None

    @staticmethod
    def verify_password(stored_hash, password):
        return check_password_hash(stored_hash, password)
