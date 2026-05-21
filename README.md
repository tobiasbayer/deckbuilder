# Commander Deckbuilder

This is a learning project for [langchain4j](https://docs.langchain4j.dev). 
It offers an AI-driven commander deck builder for the trading card game [Magic: The Gathering](https://magic.wizards.com).

## Usage

Example query:

```
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "I want to build a Commander deck around Byrke, Long Ear of the Law. What are some good creatures that synergize well? Prefer creatures from the Bloomburrow set."}' \
  | jq -r '.reply' | glow -
```