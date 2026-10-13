# Gemini integration

This branch adds a lightweight Gemini HTTP client and a background worker to demonstrate integration with Google Generative Language / Gemini endpoints.

Setup
1. Add your API key to local.properties in the project root (DO NOT commit this file):

   GEMINI_API_KEY=YOUR_API_KEY_HERE

   Optionally override the base URL (for private endpoints or testing):
   GEMINI_BASE_URL=https://generativelanguage.googleapis.com

2. The app load will inject GEMINI_API_KEY and GEMINI_BASE_URL into BuildConfig automatically.

3. Example usage
   - The MainActivity UI in this skeleton saves input to memory. The GeminiBackgroundWorker will read the latest note and send a short prompt to Gemini, storing the reply.

Notes & Security
- Never commit your API keys. Use local.properties or CI secrets for automation.
- For production, use OAuth 2.0 / service accounts (recommended) rather than API keys.
- This client is a thin wrapper and attempts to be resilient to different response shapes; adapt it to the exact Gemini API contract your project targets.
