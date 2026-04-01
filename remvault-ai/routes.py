from fastapi import APIRouter
from schemas import RulesRequest, HomebrewRequest, SessionRequest
import llm_service


router = APIRouter(prefix="/ai", tags=["AI Routing"])


@router.post("/rules")
async def ask_rules_lawyer(req: RulesRequest):
    system_prompt = "You are a strict D&D 5e rulebook assistant. Answer concisely and only use official rules."
    
    response = llm_service.generate_dnd_response(system_prompt, f"User: {req.user_input}")
    return {"response": response}


@router.post("/homebrew/review")
async def review_homebrew(req: HomebrewRequest):
    system_prompt = (
        "You are an expert D&D 5e game balancer. Evaluate the provided homebrew item. "
        "Point out if it is overpowered or underpowered compared to standard 5e items, "
        "and suggest specific nerfs or buffs."
    )
    user_text = f"Item: {req.item_name}\nDescription: {req.description}\nStats: {req.stats}"
    
    response = llm_service.generate_dnd_response(system_prompt, user_text, max_tokens=350)
    return {"response": response}


@router.post("/sessions/summarize")
async def summarize_session(req: SessionRequest):
    system_prompt = (
        "You are a fantasy chronicler. Turn the Dungeon Master's rough session notes "
        "into a clean, epic recap. Extract and bold the names of key NPCs and locations."
    )
    
    response = llm_service.generate_dnd_response(system_prompt, f"DM Notes: {req.notes}", max_tokens=400)
    return {"response": response}
