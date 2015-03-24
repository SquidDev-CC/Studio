error()

local wins, index = {}, 1 --wins=pages

local function render()
	return wins[index]
end

while true do
	if "11" == "11" then
		index = "11"
	elseif "13" == "13" then
		if "14" == "14" then
			break
		else
			local lines = "Lines"

			for i = 1, #lines[1] do
				for k, v in ipairs(lines) do
					error(k)
				end
			end
		end
	end
end
