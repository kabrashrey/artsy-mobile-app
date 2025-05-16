# 🎨 Artist Search Android App

## 📌 Project Overview
This android application allows users to search for artists, explore their artwork, discover similar artists, and browse artwork categories. Users can register, log in, and mark their favorite artists for quick access. Authentication is managed using JWT tokens, and encrypted user data is stored in a MongoDB database. The database consists of three collections: one for storing Artsy API tokens and expiry data, one for user information (name, email, password, ID), and one for user favorites.

## 🔍 Features
- Search for artists by name.
- View artist details and their artworks.
- Discover similar artists.
- Browse artwork categories.
- User authentication (Login/Register) with JWT tokens.
- Add/Remove artists from favorites.
- Timer for each favorited artist showing how long ago they were added.
- Global notifications for login, logout, and favorite actions.
- Uses Access Token to initiate auto logout after 1 hour.
- Secrets managed using Google Cloud Secret Manager

## 🔧 Tech Stack
- **Frontend:** (Mobile App): Kotlin (Android SDK)
- **Backend:** Node.js (Express.js, TypeScript)
- **Database:** MongoDB
- **API Data Source:** [Artsy API](https://www.artsy.net/)
- **State Management:** ViewModel + LiveData/StateFlow

## 🔗 API Integration
The project fetches artist data from **Artsy.com**, processes the responses, and renders them in the UI.

## 📜 Usage
1. Use the search bar to find an artist.
2. Click on an artist to view their details, artworks, similar artists, and categories.
3. Log in to mark an artist as a favorite.
4. Access your list of favorite artists anytime!

## 📝 License
This project is licensed under the MIT License.

🎨 Happy Searching!


📹 App Demo Video:
https://drive.google.com/file/d/1LkrYtIFDRZyQoYiZP2x5OWPKbOQObdij/view?usp=sharing
