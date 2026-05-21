# Commander Deckbuilder

This is a learning project for [langchain4j](https://docs.langchain4j.dev). 
It offers an AI-driven commander deck builder for the trading card game [Magic: The Gathering](https://magic.wizards.com).

## Usage

Example queries:

# Rules question → should cite rule numbers from the PDF
```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "Does deathtouch work with trample damage assignment?"}' \
  | jq -r '.reply' | glow -
```

# Deck building + rules combo
```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "I want to build an Atraxa proliferate deck. What does proliferate actually do exactly according to the rules, and which creatures abuse it best?"}' \
  | jq -r '.reply' | glow -
```

# Edge case rules question
```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "If I copy a spell with cascade, does the copy also cascade?"}' \
  | jq -r '.reply' | glow -
```