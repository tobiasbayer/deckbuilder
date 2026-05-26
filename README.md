# Commander Deckbuilder

This is a learning project for [langchain4j](https://docs.langchain4j.dev).
It offers an AI-driven commander deck builder for the trading card
game [Magic: The Gathering](https://magic.wizards.com).

## Running the service
```
./gradlew bootRun
```

When using the OpenAI provider, make sure to set your OpenAI API key before running the service (`export OPENAI_API_KEY=sk-...`).

You can switch providers in `application.yml` and configure them in `application-openai.yml` and `application-ollama.yml`.

## Usage


### Example queries

Rules question → should cite rule numbers from the PDF

```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "Does deathtouch work with trample damage assignment?", "sessionId": "1"}' \
  | jq -r '.reply' | glow -
```

Deck building + rules combo

```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "I want to build an Atraxa proliferate deck. What does proliferate actually do exactly according to the rules, and which creatures abuse it best?", "sessionId": "2"}' \
  | jq -r '.reply' | glow -
```

Edge case rules question

```
curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{"message": "If I copy a spell with cascade, does the copy also cascade?", "sessionId": "3"}' \
  | jq -r '.reply' | glow -
```

Streaming output

```
curl -N -X GET "http://localhost:8080/api/chat/stream?sessionId=s1&message=Suggest+cards+with+the+ramp+function+for+Atraxa" 
```

### Testing a multi-turn conversation

```
SESSION="atraxa-build-1"

# Step 1: Start the deck
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"I want to build Atraxa, Praetors Voice proliferate. Suggest 10 creatures.\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -

# Step 2: Refine without repeating context, agent remembers Atraxa
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Too expensive. Keep only creatures under 4 CMC and suggest replacements for the rest.\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -

# Step 3: Ask about a rule interaction
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"By the way, if I proliferate with Atraxa during my end step, does that trigger end step abilities again?\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -

# Step 4: Back to deck building, agent still remembers everything
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Great. Now suggest 5 planeswalkers that synergize with what you already suggested.\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -
```

### Testing structured output

```
SESSION="atraxa-build-2"

# Step 1: Discuss and refine via chat
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"I want Atraxa proliferate, budget around 150 USD, no infinite combos\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -

curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Focus on +1/+1 counters and planeswalkers. I already own Sol Ring and Doubling Season.\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -

# Step 2: When happy, generate the full structured deck
# The agent uses the conversation history to respect all the constraints above
curl -X POST http://localhost:8080/api/deck \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"Build the full deck based on everything we discussed.\", \"sessionId\": \"$SESSION\"}" \
  | jq .
```

### Testing guardrails

```
SESSION="pizza"

curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d "{\"message\": \"What's the best pizza recipe?\", \"sessionId\": \"$SESSION\"}" \
  | jq -r '.reply' | glow -
```