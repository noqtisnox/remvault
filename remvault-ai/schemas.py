from pydantic import BaseModel


class RulesRequest(BaseModel):
    user_input: str


class HomebrewRequest(BaseModel):
    item_name: str
    description: str
    stats: str


class SessionRequest(BaseModel):
    notes: str
