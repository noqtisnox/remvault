from fastapi import FastAPI
from pydantic import BaseModel
from llama_cpp import Llama

app = FastAPI(title="RemVault AI API", description="API for RemVault AI services", version="1.0.0")

print("Loading Mistral model into memory... This might take a few seconds.")
llm = Llama(
    model_path="./models/mistral-7b-instruct-v0.2.Q4_K_M.gguf",
    n_ctx=2048,
    verbose=False
)
print("Model loaded successfully!")


class PromptRequest(BaseModel):
    user_input: str


@app.post("/ai/rules")
async def ask_rules_lawyer(req: PromptRequest):
    """
    Ask the Rules Lawyer for D&D rules clarifications, interpretations, or advice.
    The Rules Lawyer is an expert in Dungeons & Dragons rules and can provide detailed explanations and guidance
    on various game mechanics, character abilities, spell interactions, and more.
    Whether you have a specific question about a rule or need help understanding how certain mechanics work together,
    the Rules Lawyer is here to assist you with accurate and comprehensive information.
    """
    system_prompt = "You are a strict D&D 5e rulebook assistant. Answer concisely and only use official rules."

    full_prompt = f"<s>[INST] {system_prompt}\n\nUser: {req.user_input} [/INST]"

    output = llm(
        full_prompt,
        max_tokens=150,
        stop=["</s>"],
        echo=False
    )

    generated_text = output["choices"][0]["text"].strip()
    return {"response": generated_text}
