local function Code(name, g, max, maxt, yield, fn, ...)
	if type(name) ~= "string" then error("(1)Expected string as 'name'.", 2) end
	if type(max) ~= "number" or max < 1 then error("(3)Expected number greater than 1.", 2) end
	g = g or "Graphics"
	maxt = maxt / 100 --maxt is in seconds, convert to 1/100 of its value for easy comparison with os.time() (better than converting os.time() on every single comparison)
	if yield == nil then yield = true end

	local n = {} --new (instance)
	local st, t, r = 0, 0, false --start time,time,running
	local calls = 0 --actual b. function calls successfully performed
	--TODO/WARN loops completed or function calls? (diff. position)

	local args = { ... }
	if type(fn) ~= "function" and type(fn) ~= "thread" then
		error("(5)Expected function or thread, got " .. type(fn) .. ".", 2)
	end
end
