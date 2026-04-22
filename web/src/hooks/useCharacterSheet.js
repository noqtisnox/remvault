import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client.js";

export function useCharacterSheet() {
  const { id } = useParams();
  const [sheet, setSheet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.characters
      .get(id)
      .then((s) => {
        setSheet(s);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  return { sheet, loading, error, setSheet };
}

export function useRoller() {
  const [rollResult, setRollResult] = useState(null);
  const [isRolling, setIsRolling] = useState(false);

  const handleRoll = async (rollName, modifier) => {
    setIsRolling(true);
    setRollResult(null);
    try {
      const expression =
        modifier >= 0 ? `1d20+${modifier}` : `1d20${modifier}`;
      const response = await api.dice.roll({ expression });
      setRollResult({ name: rollName, ...response });
    } catch (err) {
      alert(`Roll failed: ${err.message}`);
    } finally {
      setIsRolling(false);
    }
  };

  return { rollResult, isRolling, handleRoll, setRollResult };
}
