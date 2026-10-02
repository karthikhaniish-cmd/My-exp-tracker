from models.base import get_db

class Recurring:
    @staticmethod
    def create(user_id, name, amount, category, frequency, start_date, next_date):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO recurring_transactions (user_id, name, amount, category, frequency, start_date, next_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, (user_id, name.strip(), float(amount), category.strip(), frequency.upper(), start_date, next_date))
        conn.commit()
        r_id = cursor.lastrowid
        conn.close()
        return Recurring.find_by_id(r_id, user_id)

    @staticmethod
    def find_by_id(r_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM recurring_transactions WHERE id = ? AND user_id = ?", (r_id, user_id))
        row = cursor.fetchone()
        conn.close()
        return dict(row) if row else None

    @staticmethod
    def get_all(user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM recurring_transactions WHERE user_id = ? ORDER BY next_date ASC", (user_id,))
        rows = cursor.fetchall()
        conn.close()
        return [dict(r) for r in rows]

    @staticmethod
    def delete(r_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM recurring_transactions WHERE id = ? AND user_id = ?", (r_id, user_id))
        conn.commit()
        deleted = cursor.rowcount > 0
        conn.close()
        return deleted
