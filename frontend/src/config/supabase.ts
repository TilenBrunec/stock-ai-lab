import { createClient } from '@supabase/supabase-js';

const url = import.meta.env.VITE_SUPABASE_URL;
const key = import.meta.env.VITE_SUPABASE_ANON_KEY;

if (!url || !key) {
  throw new Error('Manjka VITE_SUPABASE_URL ali VITE_SUPABASE_ANON_KEY v .env.local');
}

export const supabase = createClient(url, key);