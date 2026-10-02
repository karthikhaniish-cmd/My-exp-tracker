from models.base import get_db

class Budget:
    @staticmethod
    def create(user_id, category, amount, month, year):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT id FROM budgets WHERE user_id = ? AND category = ? AND month = ? AND year = ?",
                       (user_id, category, month, year))
        existing = cursor.fetchone()
        if existing:
            cursor.execute("UPDATE budgets SET amount = ? WHERE id = ?", (float(amount), existing['id']))
            b_id = existing['id']
        else:
            cursor.execute("""
                INSERT INTO budgets (user_id, category, amount, month, year)
                VALUES (?, ?, ?, ?, ?)
            """, (user_id, category, float(amount), month, year))
            b_id = cursor.lastrowid
        conn.commit()
        conn.close()
        return Budget.find_by_id(b_id, user_id)

    @staticmethod
    def find_by_id(b_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM budgets WHERE id = ? AND user_id = ?", (b_id, user_id))
        row = cursor.fetchone()
        conn.close()
        return dict(row) if row else None

    @staticmethod
    def get_all(user_id, month=None, year=None):
        conn = get_db()
        cursor = conn.cursor()
        if month and year:
            cursor.execute("SELECT * FROM budgets WHERE user_id = ? AND month = ? AND year = ?", (user_id, month, year))
        else:
            cursor.execute("SELECT * FROM budgets WHERE user_id = ? ORDER BY category ASC", (user_id,))
        rows = cursor.fetchall()
        conn.close()
        return [dict(r) for r in rows]

    @staticmethod
    def delete(b_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM budgets WHERE id = ? AND user_id = ?", (b_id, user_id))
        conn.commit()
        deleted = cursor.rowcount > 0
        conn.close()
        return deleted
