# 📱 Connect — A Modern Social Media App

**Connect** is a next-gen social media platform designed to bring people together. Share thoughts, moments, and media — all in a sleek and intuitive interface. Built with ❤️ for Android, it integrates deeply with Firebase to deliver real-time and seamless social experiences.

![Platform](https://img.shields.io/badge/Platform-Android-lightseagreen) &nbsp;
![API Level](https://img.shields.io/badge/API-21%2B-steelblue) &nbsp;
![Language](https://img.shields.io/badge/Language-Kotlin-orange)
![License](https://img.shields.io/badge/License-MIT-blue) &nbsp;
![Firebase](https://img.shields.io/badge/Backend-Firebase-yellow) &nbsp;
![Contributions](https://img.shields.io/badge/Contributions-Welcome-purple)


## 📸 Screenshots 


Here’s a quick peek into the Connect app experience:

<table>
  <tr>
    <td align="center">
      <p><strong>Create Account</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/create_account_screen.webp?raw=true" width="200"/>
    </td>
    <td align="center">
      <p><strong>Home</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/home_screen.webp?raw=true" width="200"/>
    </td>
    <td align="center">
      <p><strong>Add Comment</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/add_comment_screen.webp?raw=true" width="200"/>
    </td>
    <td align="center">
      <p><strong>Search User</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/search_user_screen.webp?raw=true" width="200"/>
    </td>
  </tr>
  <tr>
    <td align="center">
      <p><strong>Settings & Privacy</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/settings_privacy_screen.webp?raw=true" width="200"/>
    </td>
     <td align="center">
      <p><strong>Add Post</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/create_post_screen.webp?raw=true" width="200"/>
    </td>
    <td align="center">
      <p><strong>Profile</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/profile_screen.webp?raw=true" width="200"/>
    </td>
    <td align="center">
      <p><strong>Chat</strong></p>
      <img src="https://github.com/teamproject2k/Connect/blob/release/screenshots_and_demo/chat_screen.webp?raw=true" width="200"/>
    </td>
  </tr>
</table>



## 🚀 Features

- 🔐 **Firebase Authentication** — Login & signup with phone number  
- 🖼️ **Media Sharing** — Upload photos and videos  
- 📝 **Post & Story** — Share thoughts as posts or ephemeral stories  
- 💬 **Real-Time Chat** — Instant messaging using Firebase Realtime Database / Firestore  
- 📰 **Feed & Timeline** — Scroll through updates from people you follow  
- 🔔 **Push Notifications** — Firebase Cloud Messaging (FCM) for real-time alerts  
- 🧑‍🤝‍🧑 **Follow System** — Follow others and connect with friends  

### 🔒 Privacy Controls
- 🙈 Show/hide **friend list**, **gender**, and **date of birth**  
- 🚫 Block/unblock users to control your experience

## 🛠️ Tech Stack

### 🧱 Core
- **Kotlin**
- **Jetpack Compose** – Modern declarative UI framework
- **Coroutines & Flow** – For async and reactive programming
- **Room Database** – Local data persistence

### 🔌 Dependency Injection
- **Dagger Hilt** – Dependency injection for Android

### ☁️ Backend & Realtime
- **Firebase** – Auth, Firestore, Realtime DB, Cloud Messaging (FCM), Storage, Crashlytics

### 🎨 UI & Media
- **Material 3 UI Components** – Modern UI toolkit
- **Coil** – Image loading for Compose
- **Palette** – Extract prominent colors from images
- **ExoPlayer** – Media playback for audio/video
- **RomeCosta Navigation** – Type-safe navigation for Compose

### 🎤 Input & Camera
- **Google Speech-to-Text API** – Convert voice to text
- **CameraX** – Jetpack library for camera access

