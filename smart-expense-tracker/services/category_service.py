class CategoryService:
    KEYWORD_MAP = {
        "Food": [
            "swiggy", "zomato", "dinner", "lunch", "breakfast", "cafe", "restaurant",
            "starbucks", "food", "groceries", "burger", "pizza", "coffee", "tea",
            "snack", "subway", "mcdonald", "kfc", "domino", "biryani", "supermarket"
        ],
        "Transport": [
            "uber", "ola", "metro", "bus", "train", "flight", "petrol", "diesel",
            "fuel", "taxi", "rapido", "cab", "parking", "toll", "commute", "auto"
        ],
        "Shopping": [
            "amazon", "flipkart", "myntra", "clothes", "shoes", "headphones", "gadget",
            "shopping", "mall", "electronics", "zara", "h&m", "ikea", "purchase"
        ],
        "Bills": [
            "electricity", "bill", "water", "wifi", "broadband", "gas", "recharge",
            "airtel", "jio", "rent", "maintenance", "utility", "insurance", "emi"
        ],
        "Entertainment": [
            "netflix", "spotify", "prime", "movie", "cinema", "game", "gaming",
            "steam", "concert", "theatre", "disney", "hotstar", "party"
        ],
        "Health": [
            "doctor", "medicine", "pharmacy", "gym", "hospital", "dentist",
            "health", "clinic", "lab", "apollo", "fitness", "supplements"
        ],
        "Education": [
            "course", "udemy", "tuition", "book", "school", "college",
            "exam", "education", "coursera", "training", "books"
        ],
        "Travel": [
            "hotel", "resort", "airbnb", "trip", "vacation", "flight", "tour",
            "travel", "booking.com", "makemytrip", "holiday"
        ],
        "Salary": [
            "salary", "paycheck", "wages", "bonus", "payroll", "stipend"
        ],
        "Freelance": [
            "freelance", "client", "upwork", "fiverr", "consulting", "contract", "gig"
        ],
        "Investment": [
            "dividend", "stocks", "mutual fund", "crypto", "interest", "investment"
        ]
    }

    @classmethod
    def detect(cls, description: str):
        if not description:
            return None
        lower = description.lower()
        for category, keywords in cls.KEYWORD_MAP.items():
            for kw in keywords:
                if kw in lower:
                    default_type = "INCOME" if category in ["Salary", "Freelance"] else "EXPENSE"
                    return {"category": category, "type": default_type}
        return None
