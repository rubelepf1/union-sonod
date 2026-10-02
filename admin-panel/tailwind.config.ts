import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        bdGreen: {
          50: '#e6f5f0',
          100: '#ccebe1',
          200: '#99d7c3',
          500: '#008763',
          600: '#006A4E',
          700: '#00543e',
          800: '#003f2e',
          900: '#00291e',
        },
        bdRed: {
          50: '#fef2f2',
          100: '#fee2e2',
          500: '#F42A41',
          600: '#dc2626',
          700: '#b91c1c',
        },
      },
      fontFamily: {
        bangla: ['var(--font-noto-bengali)', 'Noto Sans Bengali', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
export default config;
