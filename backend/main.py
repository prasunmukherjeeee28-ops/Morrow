import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from google import genai

app = FastAPI(title="Morrow Backend")

@app.get("/")
def health():
    return {"status": "online", "app": "Morrow"}

class ChatRequest(BaseModel):
    message: str

@app.post("/chat")
def chat(request: ChatRequest):
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="AI key not configured")

    try:
        client = genai.Client(api_key=api_key)
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=request.message
        )
        return {"reply": response.text or "No response received"}
    except Exception:
        raise HTTPException(status_code=502, detail="AI provider request failed")
