from models.base import get_db

class Transaction:
    @staticmethod
    def create(user_id, t_type, amount, description, category, payment_method, date, notes=""):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("""
            INSERT INTO transactions (user_id, type, amount, description, category, payment_method, date, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, (user_id, t_type.upper(), float(amount), description.strip(), category.strip(), payment_method.strip(), date, notes.strip()))
        conn.commit()
        t_id = cursor.lastrowid
        conn.close()
        return Transaction.find_by_id(t_id, user_id)

    @staticmethod
    def find_by_id(t_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM transactions WHERE id = ? AND user_id = ?", (t_id, user_id))
        row = cursor.fetchone()
        conn.close()
        return dict(row) if row else None

    @staticmethod
    def get_all(user_id, t_type=None, category=None, date_filter=None, search=None, sort="newest"):
        conn = get_db()
        cursor = conn.cursor()
        query = "SELECT * FROM transactions WHERE user_id = ?"
        params = [user_id]

        if t_type and t_type.upper() in ["INCOME", "EXPENSE"]:
            query += " AND type = ?"
            params.append(t_type.upper())

        if category and category != "All":
            query += " AND category = ?"
            params.append(category)

        if search:
            query += " AND (description LIKE ? OR notes LIKE ? OR category LIKE ? OR payment_method LIKE ?)"
            term = f"%{search}%"
            params.extend([term, term, term, term])

        if sort == "oldest":
            query += " ORDER BY date ASC, id ASC"
        elif sort == "highest":
            query += " ORDER BY amount DESC"
        elif sort == "lowest":
            query += " ORDER BY amount ASC"
        else:
            query += " ORDER BY date DESC, id DESC"

        cursor.execute(query, params)
        rows = cursor.fetchall()
        conn.close()
        return [dict(r) for r in rows]

    @staticmethod
    def update(t_id, user_id, t_type, amount, description, category, payment_method, date, notes=""):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("""
            UPDATE transactions
            SET type = ?, amount = ?, description = ?, category = ?, payment_method = ?, date = ?, notes = ?
            WHERE id = ? AND user_id = ?
        """, (t_type.upper(), float(amount), description.strip(), category.strip(), payment_method.strip(), date, notes.strip(), t_id, user_id))
        conn.commit()
        conn.close()
        return Transaction.find_by_id(t_id, user_id)

    @staticmethod
    def delete(t_id, user_id):
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM transactions WHERE id = ? AND user_id = ?", (t_id, user_id))
        conn.commit()
        deleted = cursor.rowcount > 0
        conn.close()
        return deleted
