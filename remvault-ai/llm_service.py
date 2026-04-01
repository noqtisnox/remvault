from llama_cpp import Llama

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
    
    output = llm(
        full_prompt, 
        max_tokens=max_tokens, 
        stop=["</s>"], 
        echo=False
    )
    
    return output["choices"][0]["text"].strip()
