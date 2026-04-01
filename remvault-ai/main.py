from fastapi import FastAPI
from routes import router


app = FastAPI(title="RemVault AI Microservice")
app.include_router(router)


@app.get("/health")
async def health_check():
    """A simple endpoint for Ktor or Docker to verify the service is running."""
    return {"status": "online", "model": "Mistral-7B-Instruct"}
