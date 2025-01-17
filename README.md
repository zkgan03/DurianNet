# DurianNet
DurianNet is a Final Year Project that implements a Client-Server Model using Kotlin (Android) and ASP.NET Core 8 (Backend). It features an AI-powered durian type recognition system leveraging Ultralytics YOLOv8 for object detection and a chatbot powered by Ollama Llama 3.1 with LangChain C# using RAG architecture.


## 🌟 Key Features
### 🔍 Dual Mode Detection
1. Focus Vision – High-accuracy detection with detailed classification.
2. Instant Detect – Fast, real-time object detection for quick results.

### 🚀 Two Detection Methods
1. Local Detection – Runs directly on the device using an optimized AI model for offline processing.
2. Server-Based Detection – Leverages a powerful AI model that built in Server for low-end devices, requiring a stable network connection for optimal performance.

### 🤖 AI Chatbot
1. Uses Ollama Llama 3.1 for intelligent responses.
2. Powered by LangChain C# with RAG architecture for accurate knowledge retrieval.

## Versions
1. IDE
    - Android Studio Ladybug | 2024.2.1
    - Visual Studio 2022

2. Framework / Project
    - .NET 8.0+
    - SQL Server Management Studio 19 / 20
    - CUDA 11.8

## Installation Guide

1. Database setup
   - Install [SQL 2022 Express](https://www.microsoft.com/en-my/sql-server/sql-server-downloads)
   - Run command `dotnet ef database update` in `./aspnet_server/DurianNet`.
