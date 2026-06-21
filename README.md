<p align="center">
  <img src="screenshots/banner.png" alt="LN Translator Banner" width="100%">
</p>

# LN Translator

LN Translator is a native Android application that allows you to translate Japanese Light Novels and text directly on your device's screen. Designed for a seamless and immersive reading experience, it removes language barriers by overlaying high-quality translations directly on top of your target reading application.

<p align="center">
  <a href="https://github.com/PaoloESAN/LN-Translator-Mobile/releases/latest/download/LNTranslator.apk">
    <img src="https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android" alt="Download APK">
  </a>
</p>

> [!NOTE]
> This project is not intended to replace Light Novel scanlations or official translations. Instead, it aims to provide a fast alternative so readers can enjoy newly released Japanese Light Novels immediately upon publication.

---

## Demo

https://github.com/user-attachments/assets/4e8be96e-56a1-4a91-9900-e745a05d8480

---

## Features

- **On-Screen Overlay**: Translate text instantly without switching apps or leaving your reader.
  - **Customizable Styling**: Dynamically adjust font size, line spacing, and typeface options.
  - **History Navigation**: Move back and forth through your current translation session using the previous/next control buttons.
- **Saved Translations & History**:
  - **Local Session Archive**: Keep a local database of all translated pages to revisit your reading history at any time.
  - **Editing & Correction**: Manually modify, correct, or crop OCR text and translation results directly in the page editor.
  - **Export & Sharing**: Export translated novel pages, extracted illustrations, or entire sessions to share with others.
- **Smart API Key Rotation**: Configure up to 5 Gemini API keys. The app automatically cycles through them if rate limits are hit or failures occur.
- **Work-Specific Context (Prompts)**: Define custom prompts containing specific terms, character names, and details about the novel to guide the translation context.
- **Dual Translation Modes**:
  - **Vision-based (Gemini Vision)**: Processes screen captures directly to preserve visual context and layout.
  - **Text-based (OCR + Translation)**: Extracts text locally via OCR first, then sends the plain text to the LLM for precise translation.
- **Multi-Language UI**: Full localization in English and Spanish, automatically adapting to the device system language.

---

## Requirements

- **Android Version**: Android 10 (API Level 29) or higher.
- **Permissions Required**:
  - **Display over other apps**: Required to display the floating window and translation overlay.
  - **Screen Capture**: Required to read the Japanese text on your screen and send it to the AI model.
  - **Internet Connection**: Required to send the translation requests to the AI model.

---

## Getting Started

1. **Get an API Key**: Obtain a free API key from the [Google AI Studio](https://aistudio.google.com/api-keys).
2. **Setup the Keys**: Launch the application, navigate to the Settings panel, and input your Gemini API keys.
3. **Apply Context (Optional)**: Type custom instructions on the home screen to steer the AI output.
4. **Start the Service**: Tap the Start Translator button on the home screen and grant the requested overlay and screen recording permissions.
5. **Set the Target Scope**: In the system permission prompt, we recommend selecting **Share one app** and choosing your target reader. This works seamlessly with web browsers and official Japanese e-book stores, such as [BookWalker Japan](https://bookwalker.jp/st1), [Rakuten Kobo Japan](https://books.rakuten.co.jp/book/lightnovel/), and Amazon Kindle Japan.
6. **Translate**: Tap the floating overlay trigger button to translate the current screen!

---

## How It Works

### Privacy and Security

Your privacy is a priority. Your API keys and configuration data are stored securely on your local device using Android DataStore. They are never sent to external servers other than directly to the official Google Gemini API endpoint via secure HTTPS REST requests.

### Cost and API Usage

This project is an open-source local client, not a Software as a Service (SaaS). You use your own Google Gemini API keys to handle the translation requests. The API usage is highly cost-effective; for instance, during development testing with the **Gemini 3 Flash** model, 137 full-page translation requests (including image inputs and retries) cost approximately $0.18 USD.

### Robust Key Rotation and Retries

- **Automatic Retries**: If a request fails due to temporary network issues, the app automatically retries the call up to 3 times per key.
- **Intelligent Failover**: If a key repeatedly fails (due to invalid credentials or quota limits), the app switches to the next configured key in the rotation sequence to prevent interruptions.

---

## Future Roadmap

- **On-Device Offline Translation**: Support local translation models for reading without internet connectivity.
- **Expanded LLM Providers**: Integration for OpenAI ChatGPT, xAI Grok, and Anthropic Claude APIs.
- **OCR Quality Enhancements**: Improved parsing logic for vertical Japanese layout scripts and complex overlapping text.

---

## Contributing

Contributions from the community are welcome. You can help by:

- **Prompt Optimization**: Share high-performance prompts and glossaries for specific novel genres or translation styles.
- **Localization**: Help translate the application interface into other languages.
- **Pull Requests**: Fork the repository and submit PRs for roadmap features, performance improvements, or bug fixes.

---

## License

This project is licensed under the MIT License. You are free to copy, modify, and distribute it under the license terms.
