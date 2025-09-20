# 🎵 RhytmiX
### *The Ultimate Android Music Player Experience*

[![GitHub release](https://img.shields.io/github/v/release/Cluvex/RhytmiX?style=for-the-badge&logo=android&color=3DDC84)](https://github.com/Cluvex/RhytmiX/releases)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Cluvex/RhytmiX/build.yml?style=for-the-badge&logo=github)](https://github.com/Cluvex/RhytmiX/actions)
[![License](https://img.shields.io/github/license/Cluvex/RhytmiX?style=for-the-badge&color=blue)](LICENSE)
[![Stars](https://img.shields.io/github/stars/Cluvex/RhytmiX?style=for-the-badge&color=yellow)](https://github.com/Cluvex/RhytmiX/stargazers)

**RhytmiX** is a modern, open-source Android music player built with **Jetpack Compose** and **Material You** design principles. Experience your music like never before with a clean, intuitive interface and powerful features.

[📱 Download APK](https://github.com/Cluvex/RhytmiX/releases/latest) • [🐛 Report Bug](https://github.com/Cluvex/RhytmiX/issues) • [💡 Request Feature](https://github.com/Cluvex/RhytmiX/issues)

---

## ✨ Features

### 🎨 **Beautiful Design**
- **Material You** theming with dynamic colors
- **Dark/Light** theme support with system sync
- **Smooth animations** and transitions
- **Intuitive navigation** with modern UI components

### 🎵 **Powerful Music Experience**
- **High-quality audio playback** with MediaSession support
- **Advanced music controls** (play, pause, skip, repeat, shuffle)
- **Background playback** with notification controls
- **Gapless playback** for seamless listening

### 📚 **Smart Organization**
- **Browse by** Songs, Albums, Artists, Playlists
- **Create custom playlists** with drag-and-drop
- **Favorites system** with heart animations
- **Smart search** across all your music

### 🔧 **Advanced Features**
- **Auto-resume** last played song with position
- **Multiple architecture support** (ARM64-v8a, ARMv7a)
- **Optimized performance** with lazy loading
- **No ads, no tracking** - completely free and open source

---

## 📸 Screenshots

<div align="center">

| Main Screen | Full Player | Playlists |
|:---:|:---:|:---:|
| ![Main](screenshots/main.png) | ![Player](screenshots/player.png) | ![Playlists](screenshots/playlists.png) |

| Dark Theme | Settings | Search |
|:---:|:---:|:---:|
| ![Dark](screenshots/dark.png) | ![Settings](screenshots/settings.png) | ![Search](screenshots/search.png) |

</div>

---

## 📱 Download

### 🚀 **Latest Release**
Download the latest version from our [Releases page](https://github.com/Cluvex/RhytmiX/releases/latest)

#### Architecture Support:
- **ARM64-v8a** - For modern 64-bit devices (Recommended)
- **ARMv7a** - For older 32-bit devices

### 📋 **Requirements**
- Android 7.0+ (API 24+)
- Storage permission for music access
- 50MB+ available storage

---

## 🛠️ Tech Stack

<div align="center">

| Category | Technology |
|:---:|:---|
| **Language** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white) |
| **UI Framework** | ![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white) |
| **Architecture** | ![MVVM](https://img.shields.io/badge/MVVM-Architecture-green?style=flat-square) |
| **Audio** | ![MediaSession](https://img.shields.io/badge/MediaSession-API-orange?style=flat-square) |
| **Database** | ![DataStore](https://img.shields.io/badge/DataStore-Preferences-blue?style=flat-square) |
| **Image Loading** | ![Glide](https://img.shields.io/badge/Glide-Compose-yellow?style=flat-square) |

</div>

### 🏗️ **Architecture Highlights**
- **Clean Architecture** with Repository pattern
- **Reactive Programming** with Kotlin Coroutines & Flow
- **Dependency Injection** ready structure
- **Material You** theming system
- **MediaSession** for background playback

---

## 🚀 Getting Started

### 🔧 **Build from Source**

1. **Clone the repository**
   ```bash
   git clone https://github.com/Cluvex/RhytmiX.git
   cd RhytmiX
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

### 🤖 **Automated Builds**
We use **GitHub Actions** for automated building:
- ✅ **Automatic builds** on push/PR
- ✅ **Multi-architecture** APK generation
- ✅ **Automated releases** on tags
- ✅ **Unit testing** pipeline

---

## 🎯 Roadmap

### 🔄 **Current Version (v1.0)**
- [x] Basic music playback
- [x] Material You theming
- [x] Playlist management
- [x] Background playback
- [x] Search functionality

### 🚀 **Upcoming Features**
- [ ] **Lyrics support** with synchronized display
- [ ] **Equalizer** with presets and custom bands
- [ ] **Sleep timer** with fade-out
- [ ] **Crossfade** between tracks
- [ ] **Tag editor** for music metadata
- [ ] **Last.fm scrobbling** integration
- [ ] **Android Auto** support
- [ ] **Wear OS** companion app

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### 💡 **Ways to Contribute**
- 🐛 **Report bugs** and issues
- 💡 **Suggest new features**
- 🔧 **Submit pull requests**
- 🌍 **Translate the app**
- ⭐ **Star the repository**

### 📋 **Development Setup**
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and commit: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### 🎨 **Code Style**
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **ktlint** for code formatting
- Write meaningful commit messages
- Add comments for complex logic

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```bash
Copyright (c) 2024 Cluvex Studio

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 🙏 Acknowledgments

- **Material You** design system by Google
- **Jetpack Compose** team for the amazing UI toolkit
- **Open source community** for inspiration and libraries
- **Contributors** who make this project better

---

## 📞 Contact

<div align="center">

**Cluvex Studio**

[![GitHub](https://img.shields.io/badge/GitHub-Cluvex-black?style=for-the-badge&logo=github)](https://github.com/Cluvex)
[![Email](https://img.shields.io/badge/Email-Contact-red?style=for-the-badge&logo=gmail)](mailto:cluvexstudio@gmail.com)

**Made with ❤️ for music lovers around the world**

---

⭐ **If you like RhytmiX, please consider starring the repository!** ⭐

</div>
