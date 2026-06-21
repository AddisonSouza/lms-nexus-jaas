import React from 'react'
import ReactDOM from 'react-dom/client'
import Providers from './app/providers'
import AuthBootstrap from './app/AuthBootstrap'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Providers>
      <AuthBootstrap />
    </Providers>
  </React.StrictMode>
)
