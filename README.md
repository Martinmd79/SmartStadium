````markdown
# Smart Stadium IoT Control System — Android + IoT Platform

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF)](https://kotlinlang.org/)
[![Backend](https://img.shields.io/badge/Backend-FastAPI-009688)](https://fastapi.tiangolo.com/)
[![Database](https://img.shields.io/badge/Database-InfluxDB-22ADF6)](https://www.influxdata.com/)
[![IoT](https://img.shields.io/badge/IoT-Raspberry%20Pi-C51A4A)](https://www.raspberrypi.org/)
[![Status](https://img.shields.io/badge/Status-Prototype-success)](#)

A mobile-controlled **IoT stadium management platform** that enables operators to monitor environmental conditions and remotely control stadium infrastructure in real time.

The system integrates an **Android mobile application**, **FastAPI backend services**, and **IoT sensors running on Raspberry Pi**, with telemetry stored in **InfluxDB** for real-time monitoring.

This project demonstrates a full-stack architecture combining **mobile development, backend APIs, and IoT infrastructure**.

---

# Table of Contents

- [Features](#features)
- [System Architecture](#system-architecture)
- [Application Modules](#application-modules)
- [Data Flow](#data-flow)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

# Features

- **Remote stadium control**
  - Lighting systems
  - Mechanical roof systems
  - Stadium infrastructure devices

- **Environmental monitoring**
  - Temperature
  - Humidity
  - Light levels

- **Real-time telemetry**
  - Sensor data stored in InfluxDB
  - Mobile dashboard for viewing metrics

- **IoT integration**
  - Raspberry Pi device gateway
  - REST API communication

- **Mobile control interface**
  - Android application for stadium operators
  - Modular system control screens

---

# System Architecture

The system follows a **three-layer architecture** connecting mobile applications, backend services, and IoT devices.

```mermaid
flowchart LR
  A[Android Mobile App]
  B[FastAPI Backend Services]
  C[Raspberry Pi IoT Gateway]
  D[IoT Sensors]
  E[InfluxDB Time-Series Database]

  A -->|HTTP REST API| B
  B --> C
  C --> D
  C --> E
  A -->|Telemetry Query| E
````

### Architecture Components

| Layer       | Responsibility                            |
| ----------- | ----------------------------------------- |
| Mobile App  | Control interface for stadium operators   |
| Backend API | Handles control commands and system logic |
| IoT Gateway | Communicates with sensors and devices     |
| Database    | Stores telemetry and environmental data   |

---

# Application Modules

The Android application is divided into modules representing different stadium systems.

### Field Sensors

Displays telemetry from environmental sensors deployed around the stadium.

Examples:

* Temperature monitoring
* Humidity monitoring
* Light sensor readings

---

### Lighting Control

Allows operators to remotely manage stadium lighting.

Functions include:

* Turning lights on/off
* Managing lighting zones
* Monitoring system status

---

### Mechanical Roof Control

Controls the stadium roof system.

Operators can:

* Open roof
* Close roof
* Stop roof movement

---

### Power Management

Displays power usage statistics and system performance.

Provides insight into energy consumption across stadium infrastructure.

---

# Data Flow

```mermaid
flowchart LR
  A[Android App] -->|Send Command| B[FastAPI Service]
  B -->|Execute Control| C[Raspberry Pi]
  C -->|Control Device| D[Stadium Hardware]

  D -->|Sensor Data| C
  C -->|Telemetry| E[InfluxDB]
  A -->|Query Data| E
```

---

# Technology Stack

### Programming Languages

* Kotlin
* Python

### Mobile Development

* Android SDK
* Material UI Components
* Kotlin Coroutines

### Backend

* FastAPI
* REST APIs

### IoT Infrastructure

* Raspberry Pi
* Sensor devices
* Local control services

### Data Monitoring

* InfluxDB time-series database

---

# Project Structure

```text
SmartStadium
│
├── android-app
│   ├── MainActivity.kt
│   ├── Api.kt
│   └── InfluxDBManager.kt
│
├── backend-services
│   ├── lighting-control
│   ├── roof-control
│   └── stadium-control
│
├── telemetry
│   └── sensor-data pipeline
│
└── README.md
```

---

# Requirements

* Android Studio
* Kotlin
* Raspberry Pi environment
* Python (FastAPI)
* InfluxDB

---

# Quick Start

Clone the repository:

```bash
git clone https://github.com/Martinmd79/SmartStadium.git
cd SmartStadium
```

Open the Android project in **Android Studio** and run on a device or emulator.

Ensure the backend services and telemetry database are running before launching the application.

---

# Future Improvements

Potential extensions of the system include:

* AI-based crowd monitoring using computer vision
* Real-time analytics dashboard
* Cloud-based deployment
* Predictive maintenance using sensor data
* Role-based access control for operators

---

# Author

**Martin Degani**
Software Engineering Student — Swinburne University

GitHub
https://github.com/Martinmd79

LinkedIn
https://linkedin.com/in/martin-degani

```
```
