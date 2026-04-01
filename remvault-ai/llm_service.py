import hashlib
import os
import redis
from llama_cpp import Llama

redis_host = os.getenv("REDIS_HOST", "127.0.0.1")
cache = redis.Redis(host=redis_host, port=6379, db=0, decode_responses=True)

print("Loading Mistral model into memory... This might take a few seconds.")
llm = Llama(
    model_path="./models/mistral-7b-instruct-v0.2.Q4_K_M.gguf",
    n_ctx=2048,
    verbose=False 
)
print("Mistral loaded successfully!")


def generate_dnd_response(system_prompt: str, user_input: str, max_tokens: int = 250) -> str:
    """
    Wraps the user input and system prompt in Mistral's required [INST] tags
    and generates the response.
    """
    full_prompt = f"<s>[INST] {system_prompt}\n\n{user_input} [/INST]"
    
    prompt_hash = hashlib.sha256(full_prompt.encode('utf-8')).hexdigest()
    cache_key = f"ai_response:{prompt_hash}"

    cached_result = cache.get(cache_key)
    if cached_result:
        print("Cache hit for prompt.")
        return cached_result
    print("Cache miss for prompt. Generating response...")

    output = llm(
        full_prompt, 
        max_tokens=max_tokens, 
        stop=["</s>"], 
        echo=False
    )

    generated_text = output["choices"][0]["text"].strip()

    cache.set(cache_key, generated_text, ex=86400)
    
    return generated_text
