import os
from dotenv import load_dotenv

load_dotenv()

BASE_DIR = os.path.abspath(os.path.dirname(__file__))

class Config:
    SECRET_KEY = os.getenv("FLASK_SECRET_KEY", "smart-expense-tracker-secret-key-default")
    DB_PATH = os.path.join(BASE_DIR, "database", "database.db")
    AI_API_KEY = os.getenv("AI_API_KEY", "")
    AI_MODEL = os.getenv("AI_MODEL", "gemini-1.5-flash")
    DEFAULT_CURRENCY = "₹"
