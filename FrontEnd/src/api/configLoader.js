// src/api/configLoader.js

let cachedConfig = null;

export async function loadConfig() {
  if (cachedConfig) return cachedConfig;

  const response = await fetch('/config.xml');
  const xmlText = await response.text();
  const parser = new DOMParser();
  const xmlDoc = parser.parseFromString(xmlText, 'application/xml');

  const baseURL = xmlDoc.querySelector('baseURL')?.textContent.trim();

  if (!baseURL) {
    throw new Error("config.xml에서 baseURL을 찾을 수 없습니다.");
  }

  cachedConfig = { baseURL };
  return cachedConfig;
}
