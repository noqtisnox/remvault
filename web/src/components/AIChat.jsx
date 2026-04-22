import { useState, useEffect, useRef } from "react";
import { api } from "../api/client.js";

export default function AIChat({ sheet }) {
  const [chatMessages, setChatMessages] = useState([
    {
      role: "ai",
      text: "I'm ready. Need lore advice or rule clarifications?",
    },
  ]);
  const [chatInput, setChatInput] = useState("");
  const [isChatLoading, setIsChatLoading] = useState(false);
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

  const handleChatSubmit = async (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const userText = chatInput.trim();
    setChatMessages((prev) => [...prev, { role: "user", text: userText }]);
    setChatInput("");
    setIsChatLoading(true);

    try {
      const response = await api.ai.ask({
        message: userText,
        context: {
          name: sheet.character.name,
          class: sheet.character.characterClass,
          level: sheet.character.level,
        },
      });

      setChatMessages((prev) => [
        ...prev,
        { role: "ai", text: response.reply },
      ]);
    } catch (err) {
      setChatMessages((prev) => [
        ...prev,
        { role: "ai", text: `Error: ${err.message}` },
      ]);
    } finally {
      setIsChatLoading(false);
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: "0.75rem" }}>Companion AI</h2>
      <div
        className="card"
        style={{
          display: "flex",
          flexDirection: "column",
          height: "350px",
          padding: 0,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            flex: 1,
            overflowY: "auto",
            padding: "1rem",
            display: "flex",
            flexDirection: "column",
            gap: "0.75rem",
          }}
        >
          {chatMessages.map((msg, idx) => (
            <div
              key={idx}
              style={{
                alignSelf: msg.role === "user" ? "flex-end" : "flex-start",
                backgroundColor:
                  msg.role === "user" ? "var(--primary)" : "var(--bg)",
                color: msg.role === "user" ? "#fff" : "var(--text)",
                padding: "0.6rem 0.8rem",
                borderRadius: "8px",
                maxWidth: "85%",
                fontSize: "0.85rem",
                border: msg.role === "ai" ? "1px solid var(--border)" : "none",
              }}
            >
              {msg.text}
            </div>
          ))}
          {isChatLoading && (
            <div
              style={{
                alignSelf: "flex-start",
                fontSize: "0.8rem",
                color: "var(--muted)",
                padding: "0.5rem",
              }}
            >
              Thinking...
            </div>
          )}
          <div ref={chatEndRef} />
        </div>

        <form
          onSubmit={handleChatSubmit}
          style={{
            display: "flex",
            padding: "0.75rem",
            borderTop: "1px solid var(--border)",
            backgroundColor: "var(--bg-card)",
            gap: "0.5rem",
          }}
        >
          <input
            type="text"
            placeholder="Ask the DM..."
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
            style={{
              flex: 1,
              padding: "0.5rem",
              fontSize: "0.85rem",
              borderRadius: "4px",
              border: "1px solid var(--border)",
              backgroundColor: "var(--bg)",
            }}
            disabled={isChatLoading}
          />
          <button
            type="submit"
            className="btn-primary"
            style={{ padding: "0 0.75rem", fontSize: "0.85rem" }}
            disabled={isChatLoading || !chatInput.trim()}
          >
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
