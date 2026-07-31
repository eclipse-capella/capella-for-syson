/*
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 */
import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  build: {
    minify: mode !== 'development',
  },
  test: {
    environment: 'jsdom',
    coverage: {
      reporter: ['text', 'html'],
    },
  },
  //We define the process.env to avoid 'Uncaught ReferenceError: process is not defined'.
  //Dependencies (such as react-trello) might expect environment variables to be defined (REDUX_LOGGING in this case).
  define: {
    'process.env': { ...process.env, ...loadEnv(mode, process.cwd()) },
    'import.meta.env.VITE_APP_VERSION': JSON.stringify(process.env.npm_package_version),
  },
}));
