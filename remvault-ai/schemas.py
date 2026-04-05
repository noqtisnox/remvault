from pydantic import BaseModel, Field

class AiContext(BaseModel):
    name: str
    character_class: str = Field(alias="class") # 'class' is a reserved keyword in Python, so we alias it
    level: int

class ChatRequest(BaseModel):
    message: str
    context: AiContext

class RulesRequest(BaseModel):
    user_input: str


class HomebrewRequest(BaseModel):
    item_name: str
    description: str
    stats: str


class SessionRequest(BaseModel):
    notes: str
