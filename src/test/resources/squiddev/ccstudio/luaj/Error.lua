-- Test errors work, as do stack traces
local success, msg = pcall(error, "Kuput!", 2)
assert(not success)
assertEquals("Error:1: Kuput!", msg)
