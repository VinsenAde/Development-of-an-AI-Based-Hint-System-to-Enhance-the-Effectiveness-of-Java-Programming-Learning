from fastapi import FastAPI

app = FastAPI(title="CUE AI Hint Service")

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "ai-python-service"}