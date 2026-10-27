# Supabase Home Screen Integration Analysis

## Missing Dependencies & Configuration
To connect the Home Screen Explore section to the real Supabase `public.homescreen_explorer` table, the following dependencies and configurations are required:
1. **Networking & Serialization**:
   - `io.ktor:ktor-client-core`
   - `io.ktor:ktor-client-content-negotiation`
   - `io.ktor:ktor-serialization-kotlinx-json`
   - `io.ktor:ktor-client-cio` (or `android` / `darwin` engines)
   - `org.jetbrains.kotlinx:kotlinx-serialization-json`
2. **Supabase Configuration**:
   - Supabase Project URL (`SUPABASE_URL`)
   - Supabase Public Anon Key (`SUPABASE_ANON_KEY`) — *Never use service-role key on mobile apps.*

## Supabase REST Endpoint & Query Strategy
- **Endpoint**: `${SUPABASE_URL}/rest/v1/homescreen_explorer`
- **Query Parameters**:
  - `select=*,media_assets(id,file_path,bucket_id,public_url)` (joining media assets to resolve `title_image_asset_id`)
  - `is_active=eq.true` (filtering active records on the server side)
  - `order=sort_order.asc` (sorting by sort_order ascending)
- **Headers**:
  - `apikey: ${SUPABASE_ANON_KEY}`
  - `Authorization: Bearer ${SUPABASE_ANON_KEY}`
  - `Content-Type: application/json`

## Repository Caching & Inactive Records Handling (`ExplorerRepositoryImpl`)
1. **Successful Refresh**: Replaces the active cache entirely with newly fetched active records from Supabase, ensuring remotely deactivated records (`is_active = false`) immediately disappear from the UI.
2. **Network Failure**: Preserves the last known good cache and exposes error/refresh states to the UI without falling back to mock/fake records.
3. **Concurrency Control**: Prevents duplicate concurrent network requests using mutex or job tracking.
