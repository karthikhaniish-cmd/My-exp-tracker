import csv
import io
import json

class ExportService:
    @staticmethod
    def to_csv(transactions):
        output = io.StringIO()
        writer = csv.writer(output)
        writer.writerow(["Date", "Description", "Category", "Payment Method", "Type", "Amount", "Notes"])
        for t in transactions:
            writer.writerow([
                t['date'],
                t['description'],
                t['category'],
                t['payment_method'],
                t['type'],
                t['amount'],
                t.get('notes', '')
            ])
        return output.getvalue()

    @staticmethod
    def to_json(transactions):
        return json.dumps(transactions, indent=2)

    @staticmethod
    def parse_and_validate_csv(csv_text):
        f = io.StringIO(csv_text)
        reader = csv.reader(f)
        valid = []
        errors = []

        rows = list(reader)
        if not rows:
            return {"valid": False, "transactions": [], "errors": ["File is empty"]}

        header = rows[0]
        has_header = "Date" in header or "date" in header[0].lower()
        data_rows = rows[1:] if has_header else rows

        for idx, row in enumerate(data_rows, start=2 if has_header else 1):
            if len(row) < 6:
                errors.append(f"Row {idx}: Missing columns. Expected at least 6 fields.")
                continue

            date, desc, cat, pm, t_type, amt_str = [cell.strip() for cell in row[:6]]
            notes = row[6].strip() if len(row) > 6 else ""

            try:
                amt = float(amt_str.replace("₹", "").replace("$", "").replace(",", ""))
                if amt <= 0:
                    raise ValueError()
            except ValueError:
                errors.append(f"Row {idx}: Invalid amount '{amt_str}'.")
                continue

            clean_type = "INCOME" if "income" in t_type.lower() else "EXPENSE"

            valid.append({
                "date": date,
                "description": desc,
                "category": cat or "Other",
                "payment_method": pm or "Other",
                "type": clean_type,
                "amount": amt,
                "notes": notes
            })

        return {
            "valid": len(valid) > 0,
            "transactions": valid,
            "errors": errors
        }
